/****** Script for SelectTopNRows command from SSMS  ******/
SELECT COUNT(*)
  FROM [jsrminer].[dbo].[RDData]


SELECT DISTINCT Repository
FROM [jsrminer].[dbo].[RDData]


/****** Script for SelectTopNRows command from SSMS  ******/
SELECT [Repository]
      ,[Commit]
      ,[Refactoring]
      ,[NodeType]
      ,[Locationbefore]
      ,ISNULL([LocalNameBefore],'') AS [LocalNameBefore]
      ,[LocationAfter]
      ,ISNULL([LocalNameAfter],'') AS [LocalNameAfter]
  FROM [jsrminer].[dbo].[RDData]