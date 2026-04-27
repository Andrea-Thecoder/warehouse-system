package it.warehouse.administrator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(password) || password.length() < MIN_LENGTH) {
            return false;
        }
        return UPPERCASE.matcher(password).find()
                && DIGIT.matcher(password).find()
                && SPECIAL.matcher(password).find();
    }
}