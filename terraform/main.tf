terraform {
  required_version = ">= 1.5"
  required_providers { aws = { source = "hashicorp/aws", version = "~> 5.0" } }
}

provider "aws" { region = var.region }

variable "region"        { default = "ap-south-1" }
variable "instance_type" { default = "t3.micro" }
variable "key_name"      { description = "Existing EC2 key pair name" }
variable "admin_cidr"    { description = "Your public IP as x.x.x.x/32 for SSH (never 0.0.0.0/0)" }
variable "project"       { default = "securebank" }

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023*-x86_64"]
  }
}

# 1. Isolated Virtual Private Cloud for Banking Web & Services
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  tags                 = { Name = "${var.project}-vpc", Application = "Secure Online Banking", SDG = "SDG 9" }
}

resource "aws_internet_gateway" "igw" { vpc_id = aws_vpc.main.id }

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  tags                    = { Name = "${var.project}-public-subnet" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# 2. Hardened Security Group: Public Web Access & Restrictive Admin SSH
resource "aws_security_group" "web" {
  name        = "${var.project}-web-sg"
  description = "Firewall rules for SecureBank web application"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Public HTTP Web Traffic"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH from authorized administrative workstation only"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 3. Secure EC2 Instance with IMDSv2 and Encrypted EBS Volume
resource "aws_instance" "web" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.web.id]
  key_name               = var.key_name

  # Mandatory IMDSv2 to prevent SSRF credential exfiltration
  metadata_options {
    http_tokens = "required"
  }

  # Encrypted root block device for financial data protection at rest
  root_block_device {
    encrypted   = true
    volume_size = 10
  }

  user_data = <<-EOT
    #!/bin/bash
    dnf install -y docker
    systemctl enable --now docker
    usermod -aG docker ec2-user
  EOT

  tags = { Name = "${var.project}-web-node" }
}

# 4. Financial Audit & Transaction Ledger S3 Bucket (Private, Versioned, AES-256)
resource "aws_s3_bucket" "audit_ledger" {
  bucket_prefix = "${var.project}-audit-ledger-"
}

resource "aws_s3_bucket_versioning" "audit_ledger" {
  bucket = aws_s3_bucket.audit_ledger.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "audit_ledger" {
  bucket = aws_s3_bucket.audit_ledger.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
  }
}

resource "aws_s3_bucket_public_access_block" "audit_ledger" {
  bucket                  = aws_s3_bucket.audit_ledger.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

output "public_ip"     { value = aws_instance.web.public_ip }
output "banking_url"   { value = "http://${aws_instance.web.public_ip}" }
output "audit_bucket"  { value = aws_s3_bucket.audit_ledger.bucket }
