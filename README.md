# Inventory-Management-System

## Security Configuration

### Bootstrap Token for Admin Registration

The initial admin registration endpoint (`POST /api/v1/auth/admins`) is protected with a bootstrap token to prevent unauthorized users from registering as administrators on fresh deployments.

#### Configuration

Set the `APP_BOOTSTRAP_TOKEN` environment variable to a secure, random string:

```bash
APP_BOOTSTRAP_TOKEN=your-secure-random-token-here
```

#### Usage

When registering the first admin user, include the bootstrap token in the request body:

```json
{
  "firstName": "Admin",
  "lastName": "User",
  "username": "admin",
  "password": "securepassword",
  "email": "admin@example.com",
  "phone": "+1234567890",
  "bootstrapToken": "your-secure-random-token-here"
}
```

#### Security Notes

- The bootstrap token must be configured via the `APP_BOOTSTRAP_TOKEN` environment variable
- Without a valid bootstrap token, admin registration will be rejected
- This protection only applies to the initial admin registration on fresh deployments
- After the first admin is created, additional admins can be created by the existing admin through the admin management endpoints
- Keep the bootstrap token secret and use a strong, randomly generated value
- The implementation uses constant-time comparison to prevent timing attacks