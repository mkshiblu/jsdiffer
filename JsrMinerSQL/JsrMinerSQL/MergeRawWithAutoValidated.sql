/****** Script for SelectTopNRows command from SSMS  ******/
SELECT

cr.*
, ca.[Validation]
      ,ca.rd_validation
      ,ca.[Comment]

	  
INTO dbo.ChartJS
  FROM [dbo].ChartJs_Raw AS cr



  LEFT JOIN [dbo].ChartJs_AutoValidated AS ca ON (ca.project = cr.project AND ca.commit_Id = cr.commit_Id 
 AND cr.[Refactoring_Type] = ca. [Refactoring_Type]

  AND ca.[Name_Before] = cr.[Name_Before] 
  
  AND ca.Name_After = cr.Name_After 
  
  AND ca.[Location_Before] = cr.[Location_Before]

    AND ca.[Location_After] = cr.[Location_After]
  )  

    ORDER BY cr.project, cr.commit_id, cr.[Refactoring_Type], cr.[Name_Before], cr.Name_After, cr.[Location_Before], cr.[Location_After], [Validation], rd_validation