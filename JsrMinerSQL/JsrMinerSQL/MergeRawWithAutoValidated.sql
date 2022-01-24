/****** Script for SelectTopNRows command from SSMS  ******/
SELECT

cr.*
, ca.[Validation]
      ,ca.[FoundByRd]
      ,ca.[Comment]

	  
INTO dbo.CreateReactApp
  FROM [dbo].[CreateReactApp_Raw] AS cr



  LEFT JOIN [dbo].[CreateReactApp_AutoValidated] AS ca ON (ca.project = cr.Project AND ca.commitId = cr.commitId 
 AND cr.[RefactoringType] = ca. [RefactoringType]

  AND ca.[NameBefore] = cr.[NameBefore] 
  
  AND ca.NameAfter = cr.NameAfter 
  
  AND ca.[LocationBefore] = cr.[LocationBefore]

    AND ca.[LocationAfter] = cr.[LocationAfter]
  )  

    ORDER BY cr.project, cr.commitid, cr.[RefactoringType], cr.[NameBefore], cr.[NameAfter], cr.[LocationBefore], cr.[LocationAfter], [Validation], FoundByRd