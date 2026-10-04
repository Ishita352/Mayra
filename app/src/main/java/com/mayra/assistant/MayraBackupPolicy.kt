package com.mayra.assistant
object MayraBackupPolicy {
 fun includesSourceHistory()=true
 fun excludesSecrets()=true
 fun rule()="Backups preserve source/version history and documentation while excluding passwords, tokens, OTPs and biometric data."
}