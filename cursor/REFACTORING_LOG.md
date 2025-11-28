# Refactoring Log: multiSPOTS

## Summary
Identified as a source project for the extraction of common routines to the new `fmpTools` library.

## Operations Realized

### 1. Code Extraction Source
*   Contributed to the extraction of the `tools` package to `fmpTools`.
*   Served as a reference for the transition to a component-based architecture (Managers).

### 2. Future Steps
*   Update `pom.xml` to depend on `fmpTools`.
*   Remove extracted classes from `plugins.fmp.multiSPOTS.tools`.
*   Refactor `Experiment` and `SequenceCamData` to use the new shared managers (`ImageLoader`, `TimeManager`) from `fmpTools`.



