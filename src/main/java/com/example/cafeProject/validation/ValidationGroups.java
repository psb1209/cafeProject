package com.example.cafeProject.validation;

import jakarta.validation.groups.Default;

public class ValidationGroups {
    public interface OnCreate extends Default {}
    public interface OnUpdate extends Default {}
    public interface OnDelete extends Default {}
}
