package com.mayra.assistant
object MayraDocumentationIndex {
 fun requiredDocuments()=listOf("README.md","PROJECT_STATUS.md","START_HERE.md","WINDOWS_SETUP_BN.md","INCOME_WORK_RULES.md")
 fun complete(files:Set<String>)=requiredDocuments().all{it in files}
}