const ALLOWED_SPECIAL_CHARS = "@$!%*?&";

export function validatePassword(value: string): true | string {
  const password = value.trim();

  if (!password) {
    return "Password is required";
  }
  if (password.length < 8) {
    return "At least 8 characters";
  }
  if (!/[a-z]/.test(password)) {
    return "Must include a lowercase letter";
  }
  if (!/[A-Z]/.test(password)) {
    return "Must include an uppercase letter";
  }
  if (!/\d/.test(password)) {
    return "Must include a number";
  }
  if (!/[@$!%*?&]/.test(password)) {
    return `Must include a special character (${ALLOWED_SPECIAL_CHARS})`;
  }
  if (!/^[A-Za-z\d@$!%*?&]+$/.test(password)) {
    return `Only letters, numbers, and ${ALLOWED_SPECIAL_CHARS} are allowed`;
  }
  if (value !== password) {
    return "Remove spaces at the beginning or end of your password";
  }

  return true;
}
