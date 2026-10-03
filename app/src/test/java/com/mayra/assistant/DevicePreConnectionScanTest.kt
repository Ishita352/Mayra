package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class DevicePreConnectionScanTest {
    @Test fun maliciousScanIsNeverEligible() {
        val scan = DevicePreConnectionScan(
            DevicePreConnectionSecurityPolicy.Risk.MALICIOUS,
            "VerifiedScanner",
            1_000L,
            listOf("malicious risk")
        )
        assertFalse(scan.isConnectionEligible())
        assertTrue(scan.userMessage().contains("Critical"))
    }

    @Test fun suspiciousScanIsNeverEligible() {
        val scan = DevicePreConnectionScan(
            DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS,
            "VerifiedScanner",
            1_000L,
            listOf("suspicious activity")
        )
        assertFalse(scan.isConnectionEligible())
    }

    @Test fun unknownScanDoesNotAutoConnect() {
        val scan = DevicePreConnectionScan(
            DevicePreConnectionSecurityPolicy.Risk.UNKNOWN,
            "VerifiedScanner",
            1_000L,
            emptyList()
        )
        assertFalse(scan.isConnectionEligible())
    }

    @Test fun cleanScanCanProceedToOwnerApproval() {
        val scan = DevicePreConnectionScan(
            DevicePreConnectionSecurityPolicy.Risk.CLEAN,
            "VerifiedScanner",
            1_000L,
            emptyList()
        )
        assertTrue(scan.isConnectionEligible())
    }
}
