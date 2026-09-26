import { describe, expect, it } from "vitest";
import {
  PASSWORD_RULE,
  loginSchema,
  passwordSchema,
  pharmacienEditSchema
} from "./validationSchemas";

describe("passwordSchema (meme regle que @StrongPassword cote backend)", () => {
  it.each(["123456789", "abcdefgh", "ABCDEFG1", "Abc1234", "A1" + "a".repeat(71)])(
    "refuse %s",
    (password) => {
      const result = passwordSchema.safeParse(password);
      expect(result.success).toBe(false);
      expect(result.error?.issues.at(0)?.message).toBe(PASSWORD_RULE);
    }
  );

  it.each(["Test1234", "A1" + "a".repeat(70)])("accepte %s", (password) => {
    expect(passwordSchema.safeParse(password).success).toBe(true);
  });

  it("n'est pas appliquee a la connexion (comptes existants)", () => {
    expect(loginSchema.safeParse({ email: "a@b.fr", password: "123456" }).success).toBe(true);
  });

  it("accepte un mot de passe vide en modification de pharmacien (inchange)", () => {
    const base = { nomUser: "P", email: "p@test.com" };
    expect(pharmacienEditSchema.safeParse({ ...base, password: "" }).success).toBe(true);
    expect(pharmacienEditSchema.safeParse({ ...base, password: "abcdefgh" }).success).toBe(false);
  });
});
