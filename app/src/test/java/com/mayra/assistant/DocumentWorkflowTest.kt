package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentWorkflowTest {
    @Test
    fun plansPdfRead() {
        val result = DocumentWorkflow.plan("PDF পড়তে চাই")
        assertEquals(DocumentWorkflow.Format.PDF, result.format)
        assertTrue(DocumentWorkflow.Action.READ in result.actions)
        assertTrue(result.recognized)
    }

    @Test
    fun plansDocxEditAndStructure() {
        val result = DocumentWorkflow.plan("DOCX edit করে structure ঠিক রাখো")
        assertEquals(DocumentWorkflow.Format.DOCX, result.format)
        assertTrue(DocumentWorkflow.Action.EDIT in result.actions)
        assertTrue(DocumentWorkflow.Action.PRESERVE_STRUCTURE in result.actions)
    }

    @Test
    fun conversionEngineSupportsBasicPdfTargets() {
        assertTrue(DocumentConversionEngine.supports(DocumentConversionEngine.Source.TXT, DocumentConversionEngine.Target.PDF))
        assertTrue(DocumentConversionEngine.supports(DocumentConversionEngine.Source.DOCX, DocumentConversionEngine.Target.PDF))
        assertTrue(!DocumentConversionEngine.supports(DocumentConversionEngine.Source.DOCX, DocumentConversionEngine.Target.DOCX))
    }

    @Test
    fun structurePreservationIsExplicitlyNotReady() {
        val result = DocumentWorkflow.plan("DOCX edit করে structure ঠিক রাখো")
        assertTrue(result.message.contains("structure-preserving"))
    }

    @Test
    fun unknownRequestFailsClosed() {
        val result = DocumentWorkflow.plan("weather")
        assertEquals(DocumentWorkflow.Format.UNKNOWN, result.format)
        assertTrue(!result.recognized)
    }
}
