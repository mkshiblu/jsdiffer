/****** Script for SelectTopNRows command from SSMS  ******/
SELECT *
  FROM [jsrminer].[dbo].[RDDataOriginal] AS rdo

  LEFT JOIN dbo.RDJSDataOriginal AS rdjo ON 
  
  rdjo.[Commit] = rdo.[Commit] 
      AND rdjo.[Refactoring] = rdo.[Refactoring]
      AND rdjo.[Node_type] = rdo.[NodeType]
      AND rdjo.[Location_before] = rdo.[Locationbefore]
      AND rdjo.[Local_name_before] = rdo.[Localnamebefore]
      ANd rdjo.[Location_after] = rdo.[Locationafter]
      AND rdjo.[Local_name_after] = rdo.[Localnameafter]

	  WHERE rdjo.[Commit] IS NULL