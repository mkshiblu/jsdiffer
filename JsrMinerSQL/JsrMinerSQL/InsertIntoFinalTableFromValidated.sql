--SELECT f.*, 
UPDATE f
SET f.Validation = v.[Validation]
, f.FoundByRd = v.FoundByRd, f.comment = v.Comment
FROM dbo.CreateReactApp AS f
LEFT JOIN dbo.CreateReactApp_Validated AS v
ON (v.project = f.Project AND v.commitId = f.commitId 
	AND f.[RefactoringType] = v. [RefactoringType]
	AND v.[NameBefore] = f.[NameBefore] 
	AND v.NameAfter = f.NameAfter 
	AND v.[LocationBefore] = f.[LocationBefore]
	AND v.[LocationAfter] = f.[LocationAfter]
);

