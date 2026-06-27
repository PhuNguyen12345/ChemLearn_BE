UPDATE learning_packages
SET base_price = 300000,
    duration_days = 365
WHERE package_code IN ('GRADE_6', 'GRADE_7', 'GRADE_8', 'GRADE_9')
  AND (base_price IS NULL OR duration_days IS NULL);
