UPDATE RMData
SET Refactoring_Type  = REPLACE(REPLACE(Refactoring_Type, 'OPERATION', 'FUNCTION'), 'METHOD', 'FUNCTION')

UPDATE RMDATA
SET Refactoring_Type  = REPLACE(Refactoring_Type, '_AND_', '_')
WHERE Refactoring_Type LIKE '%_AND_%'