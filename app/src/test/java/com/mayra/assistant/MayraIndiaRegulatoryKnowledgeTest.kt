package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MayraIndiaRegulatoryKnowledgeTest {
    @Test fun coversCoreDomains() {
        val d = MayraIndiaRegulatoryKnowledge.domains()
        assertTrue(MayraIndiaRegulatoryKnowledge.Domain.CONSTITUTION in d)
        assertTrue(MayraIndiaRegulatoryKnowledge.Domain.RBI in d)
        assertTrue(MayraIndiaRegulatoryKnowledge.Domain.DIRECT_TAX in d)
        assertTrue(MayraIndiaRegulatoryKnowledge.Domain.GST in d)
    }

    @Test fun acceptsOfficialHttpsUpdateShape() {
        val s = MayraIndiaRegulatoryKnowledge.OfficialSource("Official", "https://official.example", setOf(MayraIndiaRegulatoryKnowledge.Domain.RBI))
        assertTrue(MayraIndiaRegulatoryKnowledge.accept(MayraIndiaRegulatoryKnowledge.Update(MayraIndiaRegulatoryKnowledge.Domain.RBI, s, 1L, summary = "Update")))
    }

    @Test fun rejectsEmptyUpdate() {
        val s = MayraIndiaRegulatoryKnowledge.OfficialSource("Official", "https://official.example", setOf(MayraIndiaRegulatoryKnowledge.Domain.RBI))
        assertFalse(MayraIndiaRegulatoryKnowledge.accept(MayraIndiaRegulatoryKnowledge.Update(MayraIndiaRegulatoryKnowledge.Domain.RBI, s, 0L, summary = "")))
    }

    @Test fun staleKnowledgeRequiresVerification() {
        assertTrue(MayraIndiaRegulatoryKnowledge.requiresCurrentVerification(null, 1000L))
    }
}