UPDATE Axios
SET RefactoringType  = REPLACE(REPLACE(RefactoringType, 'OPERATION', 'FUNCTION'), 'METHOD', 'FUNCTION')


