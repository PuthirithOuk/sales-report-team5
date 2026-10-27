# Refactoring FatReportController

## Smells and consequences
| Smell | Consequence |
|---|---|
| Month text goes straight into `Path.of` | `../` can escape the data folder |
| `Files.list` never closed, non-CSV files read | resource leak, junk rows |
| `split(",")`, assumes 10 columns | breaks on quoted commas, `ArrayIndexOutOfBounds` |
| No validation | negative quantity silently subtracts from revenue |
| No rounding, discount limit ignored | wrong scale, invalid rows counted |
| `catch (Exception)` shows "Error" | user can't tell what went wrong |
| `branchBox.getValue()` may be null | NPE |
| I/O and parsing in the UI handler | window freezes, untestable without JavaFX |

## Figures (2026-09, ALL, broken row present)
| | Fat controller | After refactoring |
|---|---|---|
| Revenue shown | Revenue: 26715.40 (wrong, no warning) | USD 26721.90 · 1 rejected |
| Month `2026/09` | "Error" | "'2026/09' is not a month in the form yyyy-MM" |
| Lines in the file | BEFORE | AFTER |
| Lines in the handler (`onLoad`) | about 20 | 1 |
| Tests possible without JavaFX | 0 | all rules |

## Evidence
- `lab02/screenshots/architecture-test-fail.png`: UiIndependenceTest failing on purpose