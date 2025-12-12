package com.example.cafeProject.validation;

public class ValidationGroups {
    public interface OnCreate {}
    public interface OnUpdate {}
    public interface OnDelete {}

    public interface OnWrite extends OnCreate, OnUpdate {}
    public interface OnAll extends OnWrite, OnDelete {}
}
