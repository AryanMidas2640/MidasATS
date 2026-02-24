package com.midas.consulting.model;

public  class ValidationResult {
        private final boolean isValid;
        private final String message;
        private final ValidationLevel level;

        private ValidationResult(boolean isValid, String message, ValidationLevel level) {
            this.isValid = isValid;
            this.message = message;
            this.level = level;
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(true, message, ValidationLevel.SUCCESS);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, ValidationLevel.ERROR);
        }

        public static ValidationResult warning(String message) {
            return new ValidationResult(true, message, ValidationLevel.WARNING);
        }

        public static ValidationResult info(String message) {
            return new ValidationResult(true, message, ValidationLevel.INFO);
        }

        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
        public ValidationLevel getLevel() { return level; }
        public boolean isError() { return level == ValidationLevel.ERROR; }
        public boolean isWarning() { return level == ValidationLevel.WARNING; }

        public enum ValidationLevel {
            SUCCESS, INFO, WARNING, ERROR
        }
    }