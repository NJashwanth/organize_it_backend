# Security Policy

## Reporting a Vulnerability
If you discover a security issue, please do not open a public issue.

Please report it privately to the repository owner through GitHub Security Advisories (preferred) or a private channel.

Include:
- A clear description of the issue
- Steps to reproduce
- Potential impact
- Suggested remediation (if available)

## Secrets and Credentials
- Do not commit `.env` or any real credentials.
- Use `.env.example` for shared configuration templates.
- Rotate credentials immediately if they are exposed.

## Supported Versions
Security fixes are currently applied to the latest `main` branch.
