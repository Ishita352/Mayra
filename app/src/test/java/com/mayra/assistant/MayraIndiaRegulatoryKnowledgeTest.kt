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
        val s = "https://official.example/source"
        assertTrue(MayraIndiaRegulatoryKnowledge.accept(MayraIndiaRegulatoryKnowledge.Update(MayraIndiaRegulatoryKnowledge.Domain.RBI,s,1L,"Update")))
    }
    @Test fun rejectsEmptyUpdate() {
        assertFalse(MayraIndiaRegulatoryKnowledge.accept(MayraIndiaRegulatoryKnowledge.Update(MayraIndiaRegulatoryKnowledge.Domain.RBI,"https://official.example",0L,"")))
    }
}