import { describe, expect, it } from "vitest";
import { validatePassword } from "../password";

describe("validatePassword", () => {
  it("accepts Password123@", () => {
    expect(validatePassword("Password123@")).toBe(true);
  });

  it("rejects passwords with trailing spaces", () => {
    expect(validatePassword("Password123@ ")).toBe(
      "Remove spaces at the beginning or end of your password"
    );
  });

  it("rejects unsupported special characters", () => {
    expect(validatePassword("Password123#")).toBe(
      "Must include a special character (@$!%*?&)"
    );
  });
});
