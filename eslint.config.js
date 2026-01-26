import eslint from "@eslint/js";
import tseslint from "typescript-eslint";

export default eslint.configs(
    eslint.configs.recommended,
    tseslint.configs.recommended,
)