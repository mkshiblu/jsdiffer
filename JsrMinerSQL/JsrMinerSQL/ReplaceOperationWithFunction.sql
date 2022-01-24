UPDATE [CreateReactApp_Raw]
SET RefactoringType  = REPLACE(REPLACE(RefactoringType, 'OPERATION', 'FUNCTION'), 'METHOD', 'FUNCTION')


