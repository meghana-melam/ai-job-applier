# ========================================
# AI Job Applier - GitHub Upload Script
# ========================================
# 
# Run this script to push your project to GitHub
# Make sure you've created a repository on GitHub first!
#

Write-Host "`n🚀 AI Job Applier - GitHub Upload Script`n" -ForegroundColor Cyan

# Check if Git is installed
$gitInstalled = Get-Command git -ErrorAction SilentlyContinue
if (-not $gitInstalled) {
    Write-Host "❌ Git is not installed. Please install Git first: https://git-scm.com/download/win" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Git is installed" -ForegroundColor Green

# Get GitHub username
$username = Read-Host "`nEnter your GitHub username"

# Get repository name (default: ai-job-applier)
$repoName = Read-Host "Enter repository name (default: ai-job-applier)"
if ([string]::IsNullOrWhiteSpace($repoName)) {
    $repoName = "ai-job-applier"
}

Write-Host "`n📋 Repository Details:" -ForegroundColor Yellow
Write-Host "  Username: $username" -ForegroundColor White
Write-Host "  Repository: $repoName" -ForegroundColor White
Write-Host "  URL: https://github.com/$username/$repoName`n" -ForegroundColor Cyan

$confirm = Read-Host "Is this correct? (y/n)"
if ($confirm -ne 'y') {
    Write-Host "❌ Cancelled" -ForegroundColor Red
    exit 0
}

# Check if repository exists on GitHub
Write-Host "`n⏳ Checking if repository exists on GitHub..." -ForegroundColor Yellow
$repoUrl = "https://api.github.com/repos/$username/$repoName"
try {
    $response = Invoke-RestMethod -Uri $repoUrl -Method Get -ErrorAction Stop
    Write-Host "✅ Repository exists: $($response.html_url)" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Repository not found. Please create it first:" -ForegroundColor Yellow
    Write-Host "   1. Go to https://github.com/new" -ForegroundColor White
    Write-Host "   2. Repository name: $repoName" -ForegroundColor White
    Write-Host "   3. Add description: AI-powered job application system with Gemini" -ForegroundColor White
    Write-Host "   4. Choose Public or Private" -ForegroundColor White
    Write-Host "   5. DO NOT initialize with README, .gitignore, or license`n" -ForegroundColor White
    
    $createNow = Read-Host "Have you created the repository? (y/n)"
    if ($createNow -ne 'y') {
        Write-Host "❌ Please create the repository first, then run this script again" -ForegroundColor Red
        exit 0
    }
}

Write-Host "`n🔧 Initializing Git repository...`n" -ForegroundColor Cyan

# Initialize Git if not already initialized
if (-not (Test-Path ".git")) {
    git init
    Write-Host "✅ Git repository initialized" -ForegroundColor Green
} else {
    Write-Host "✅ Git repository already initialized" -ForegroundColor Green
}

# Check for sensitive files
Write-Host "`n🔒 Checking for sensitive files..." -ForegroundColor Yellow
if (Test-Path "src\main\resources\application.properties") {
    $apiKey = Select-String -Path "src\main\resources\application.properties" -Pattern "YOUR_GEMINI_API_KEY|YOUR_TELEGRAM_BOT_TOKEN"
    if (-not $apiKey) {
        Write-Host "⚠️  WARNING: application.properties contains real API keys!" -ForegroundColor Red
        Write-Host "   This file will NOT be uploaded (it's in .gitignore)" -ForegroundColor Yellow
        Write-Host "   Users will use application.properties.template instead`n" -ForegroundColor Yellow
    } else {
        Write-Host "✅ Template values detected in application.properties" -ForegroundColor Green
    }
}

# Add all files
Write-Host "`n📦 Adding files to Git...`n" -ForegroundColor Cyan
git add .

# Show status
Write-Host "`n📄 Files to be committed:`n" -ForegroundColor Yellow
git status --short

$continueCommit = Read-Host "`nProceed with commit? (y/n)"
if ($continueCommit -ne 'y') {
    Write-Host "❌ Cancelled" -ForegroundColor Red
    exit 0
}

# Commit
Write-Host "`n💾 Committing files..." -ForegroundColor Cyan
git commit -m "Initial commit: AI Job Applier with Gemini AI

- Complete Spring Boot application with MySQL
- Google Gemini AI integration for matching and cover letters
- Telegram bot for mobile access
- Application tracker web UI
- Resume parsing from PDF
- Instant job analysis from URLs
- Manual job entry support
- Comprehensive documentation"

Write-Host "✅ Files committed" -ForegroundColor Green

# Add remote
Write-Host "`n🔗 Adding remote repository..." -ForegroundColor Cyan
$remoteUrl = "https://github.com/$username/$repoName.git"

# Check if remote already exists
$existingRemote = git remote get-url origin 2>$null
if ($existingRemote) {
    Write-Host "⚠️  Remote 'origin' already exists: $existingRemote" -ForegroundColor Yellow
    $updateRemote = Read-Host "Update to new URL? (y/n)"
    if ($updateRemote -eq 'y') {
        git remote set-url origin $remoteUrl
        Write-Host "✅ Remote updated" -ForegroundColor Green
    }
} else {
    git remote add origin $remoteUrl
    Write-Host "✅ Remote added: $remoteUrl" -ForegroundColor Green
}

# Rename branch to main
Write-Host "`n🌿 Setting default branch to 'main'..." -ForegroundColor Cyan
git branch -M main
Write-Host "✅ Branch set to 'main'" -ForegroundColor Green

# Push to GitHub
Write-Host "`n🚀 Pushing to GitHub...`n" -ForegroundColor Cyan
Write-Host "You may be prompted for GitHub credentials...`n" -ForegroundColor Yellow

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  ✅ SUCCESS! Project uploaded to GitHub!" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════════════════════════`n" -ForegroundColor Cyan
    Write-Host "🌐 Repository URL:" -ForegroundColor Yellow
    Write-Host "   https://github.com/$username/$repoName`n" -ForegroundColor Cyan
    Write-Host "📝 Next steps:" -ForegroundColor Yellow
    Write-Host "   1. Visit your repository on GitHub" -ForegroundColor White
    Write-Host "   2. Update README.md with your contact info" -ForegroundColor White
    Write-Host "   3. Add topics/tags for better discoverability" -ForegroundColor White
    Write-Host "   4. Star your own repo to promote it! ⭐`n" -ForegroundColor White
} else {
    Write-Host "`n❌ Push failed. Please check the error above." -ForegroundColor Red
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "   • Repository doesn't exist on GitHub" -ForegroundColor White
    Write-Host "   • Authentication failed (check credentials)" -ForegroundColor White
    Write-Host "   • Network connection issue`n" -ForegroundColor White
}
