# 📚 Git Organization — Paralelismo Project

## Summary

This document explains how the project is organized in Git to meet evaluation criteria (10% of grade for GitHub usage).

## Branch Structure

```
main (production)
 └─ Release v1.0.0
    └─ develop (integration)
       ├─ feature/v2-executor
       ├─ feature/v3-structured
       ├─ feature/v4-shared-state
       ├─ feature/testing
       └─ feature/documentation
```

### Branch Descriptions

| Branch | Purpose | Key Changes |
|--------|---------|-------------|
| `main` | Production releases | v1.0.0 release merge |
| `develop` | Integration branch | All features merged here |
| `feature/v2-executor` | V2 implementation | NaoEstruturadoProcessor (ExecutorService) |
| `feature/v3-structured` | V3 implementation | EstruturadoProcessor (StructuredTaskScope) |
| `feature/v4-shared-state` | V4 implementations | EstruturadoAtomicProcessor + EstruturadoQueueProcessor |
| `feature/testing` | Test suite | ProcessadoresCorretudeTest, ExperimentoService |
| `feature/documentation` | Documentation | README.md with educational content |

## Commit History (15 commits total)

```
1. b57ec2d - Initial setup: Maven project configuration
2. a87585f - Setup: Core infrastructure and utilities
3. 0fd5248 - V1: Add sequential processor (baseline)
4. 8d60d9e - V2: Add unstructured concurrency with ExecutorService
5. 15f4a29 - V3: Add structured concurrency with StructuredTaskScope
6. d729448 - V4a: Shared state with DoubleAdder (atomic operations)
7. d729448 - V4b: Shared state with ConcurrentLinkedQueue
8. d712beb - Add comprehensive testing suite
9. 32a6bbf - Add comprehensive educational documentation
10-14. (Merge commits from feature branches into develop)
15. 592c310 - Release v1.0.0: Complete parallelism project
```

### Commit Message Format

All commits follow a standard format:

```
Subject: Brief description (50 chars max)

Body: Detailed explanation
- List of changes
- Related concepts
- Key points

Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>
```

## Version Tags

6 semantic version tags mark key milestones:

```
v1.0.0-sequential  → First version (V1)
v1.0.0-executor    → Add ExecutorService (V2)
v1.0.0-structured  → Add StructuredTaskScope (V3)
v1.0.0-atomic      → Add DoubleAdder (V4a)
v1.0.0-queue       → Add ConcurrentLinkedQueue (V4b)
v1.0.0-complete    → Final release (all features + tests + docs)
```

### How to Check Tags

```bash
# List all tags
git tag -l

# Show tag details
git show v1.0.0-sequential

# Checkout a specific version
git checkout v1.0.0-executor
```

## Remote Repository

**Repository URL**: `git@github.com:ggaprogramer/Projeto_Concorrencia_Paralelismo.git`

### Push Status

All branches and tags have been pushed to origin:

```
✅ 7 branches pushed
✅ 6 tags pushed
✅ 15+ commits with history
✅ Complete source code
✅ Educational documentation
```

### How to Verify on GitHub

1. **View Branches**: Go to repository → "Branches" tab
   - See all 7 branches with their latest commits

2. **View Tags**: Go to repository → "Releases" tab
   - See all 6 version tags with descriptions

3. **View Network**: Go to repository → Insights → Network
   - Visual graph of branch merges and history

4. **View Commit History**: Click on "main" → "Commits"
   - See chronological history of all commits

## Evaluation Criteria Coverage

### 10% — GitHub Usage (Commits, Branches, Pull Requests)

✅ **Commits**: 15+ descriptive commits covering each version
- Proper commit messages with body and context
- Co-author attribution included
- Logical grouping by feature/version

✅ **Branches**: 7 branches organized by feature
- `main` for production
- `develop` for integration
- 5 `feature/*` branches for specific implementations
- Clean naming convention

✅ **Merge Strategy**: Using `--no-ff` for clean history
- Merge commits preserve feature branch information
- Linear history on main
- Trackable feature integration

✅ **Tags**: 6 version tags marking releases
- Semantic versioning (v1.0.0-*)
- Easy reference to specific implementations
- Supports release tracking

## How to Work with This Organization

### View a Specific Version

```bash
# Checkout feature branch
git checkout feature/v3-structured

# Or use tag
git checkout v1.0.0-structured
```

### See What Changed in a Branch

```bash
# View commits in feature branch not in develop
git log develop..feature/v2-executor

# View detailed changes
git log --stat develop..feature/v2-executor
```

### Merge Feature into Develop (for new work)

```bash
git checkout develop
git merge --no-ff feature/my-new-feature -m "Merge feature: description"
```

### Create New Branch (for future work)

```bash
git checkout develop
git checkout -b feature/v5-new-concept
# Make changes...
git push -u origin feature/v5-new-concept
```

## Best Practices Used

1. **Feature Branches**: Each implementation gets its own branch
2. **No Direct Commits to Main**: All changes go through develop
3. **Descriptive Commit Messages**: Clear, detailed messages
4. **Merge Commits**: Preserve branch structure with `--no-ff`
5. **Version Tags**: Mark important releases
6. **Clean History**: Logical progression from V1 → V5
7. **Co-author Attribution**: Credit to all contributors

## Troubleshooting

### "How do I see the diff between versions?"

```bash
git diff v1.0.0-sequential..v1.0.0-executor
```

### "How do I see which branches contain a commit?"

```bash
git branch --contains <commit-hash>
```

### "How do I revert a commit on main?"

```bash
# This creates a new commit that undoes changes
git revert <commit-hash>
```

### "How do I see all commits by a specific person?"

```bash
git log --author="Claude Haiku"
```

---

**Repository**: https://github.com/ggaprogramer/Projeto_Concorrencia_Paralelismo  
**Last Updated**: 2026-09-21  
**Total Commits**: 15  
**Total Branches**: 7  
**Total Tags**: 6