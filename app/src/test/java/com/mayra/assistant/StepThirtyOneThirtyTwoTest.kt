package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepThirtyOneThirtyTwoTest {
 @Test fun releaseManifestNeedsArtifacts(){assertTrue(MayraReleasePackage.ready(MayraReleasePackage.manifest().map{it.name}.toSet()))}
 @Test fun docsIndexChecksRequiredFiles(){assertTrue(MayraDocumentationIndex.complete(MayraDocumentationIndex.requiredDocuments().toSet()))}
 @Test fun backupPolicyExcludesSecrets(){assertTrue(MayraBackupPolicy.excludesSecrets())}
 @Test fun releaseGatePasses(){assertTrue(MayraReleaseGate.passed())}
 @Test fun certificateOnlyAfterAllRoadmapComplete(){assertNull(MayraProjectCertificate.issue("Gopal Basak",32,42));assertNotNull(MayraProjectCertificate.issue("Gopal Basak",42,42))}
}