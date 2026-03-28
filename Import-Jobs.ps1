# Bulk Job Import Script
# Save this as: Import-Jobs.ps1

# Method 1: Import from CSV file
function Import-JobsFromCSV {
    param(
        [string]$CsvPath = "jobs.csv"
    )
    
    Write-Host "Reading jobs from $CsvPath..." -ForegroundColor Yellow
    
    $csvJobs = Import-Csv $CsvPath
    
    $jobs = $csvJobs | ForEach-Object {
        $skillsList = if ($_.Skills) { $_.Skills.Split(',').Trim() } else { @() }
        
        @{
            title = $_.Title
            company = $_.Company
            location = $_.Location
            jobUrl = $_.JobUrl
            description = $_.Description
            experience = $_.Experience
            skills = $skillsList
            source = if ($_.Source) { $_.Source } else { "MANUAL" }
            isEasyApply = if ($_.IsEasyApply -eq "true") { $true } else { $false }
        }
    }
    
    $jsonBody = $jobs | ConvertTo-Json -Depth 3
    
    Write-Host "Uploading $($jobs.Count) jobs..." -ForegroundColor Cyan
    
    try {
        $response = Invoke-WebRequest `
            -Uri "http://localhost:8080/api/manual/add-jobs-bulk" `
            -Method POST `
            -ContentType "application/json" `
            -Body $jsonBody `
            -UseBasicParsing
        
        $result = $response.Content | ConvertFrom-Json
        Write-Host "`n✓ Success! Added $($result.count) jobs" -ForegroundColor Green
        
        return $result
    } catch {
        Write-Host "`n✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Method 2: Quick batch from arrays
function Add-JobsBatch {
    param(
        [array]$JobList
    )
    
    $jobs = $JobList | ForEach-Object {
        @{
            title = $_[0]
            company = $_[1]
            location = $_[2]
            jobUrl = $_[3]
            description = $_[4]
            skills = $_[5] -split ','
            source = if ($_[6]) { $_[6] } else { "MANUAL" }
            isEasyApply = if ($_[7]) { $true } else { $false }
        }
    }
    
    $jsonBody = $jobs | ConvertTo-Json -Depth 3
    
    Write-Host "Adding $($jobs.Count) jobs..." -ForegroundColor Cyan
    
    try {
        $response = Invoke-WebRequest `
            -Uri "http://localhost:8080/api/manual/add-jobs-bulk" `
            -Method POST `
            -ContentType "application/json" `
            -Body $jsonBody `
            -UseBasicParsing
        
        $result = $response.Content | ConvertFrom-Json
        Write-Host "✓ Added $($result.count) jobs!" -ForegroundColor Green
        
        return $result
    } catch {
        Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Example Usage:

# === OPTION A: Import from CSV ===
# Create jobs.csv with columns: Title,Company,Location,JobUrl,Description,Experience,Skills,Source,IsEasyApply
# Import-JobsFromCSV -CsvPath "C:\ai-job-applier\jobs.csv"

# === OPTION B: Quick batch from LinkedIn/Naukri ===
$jobsBatch = @(
    @("Java Developer", "TCS", "Bangalore", "https://linkedin.com/jobs/1", "Java Spring Boot MySQL", "Java,Spring Boot,MySQL", "LINKEDIN", $true),
    @("Senior Java Developer", "Infosys", "Hyderabad", "https://linkedin.com/jobs/2", "Senior Java Spring Microservices", "Java,Spring Boot,Microservices", "LINKEDIN", $true),
    @("Java Backend Engineer", "Wipro", "Pune", "https://naukri.com/jobs/3", "Java backend REST API development", "Java,REST API,MySQL", "NAUKRI", $false),
    @("Full Stack Developer", "HCL", "Chennai", "https://linkedin.com/jobs/4", "Java React full stack", "Java,React,Spring Boot", "LINKEDIN", $true),
    @("Java Architect", "Cognizant", "Mumbai", "https://linkedin.com/jobs/5", "Java architect microservices", "Java,Microservices,Docker", "LINKEDIN", $false),
    @("Java Developer", "Accenture", "Gurgaon", "https://naukri.com/jobs/6", "Java Spring Cloud development", "Java,Spring Cloud,Kubernetes", "NAUKRI", $false),
    @("Senior Backend Engineer", "Capgemini", "Bangalore", "https://linkedin.com/jobs/7", "Backend Java AWS", "Java,AWS,Docker", "LINKEDIN", $true),
    @("Java Microservices Dev", "Tech Mahindra", "Hyderabad", "https://naukri.com/jobs/8", "Microservices Java Spring", "Java,Microservices,Spring", "NAUKRI", $false),
    @("Lead Java Developer", "L&T Infotech", "Pune", "https://linkedin.com/jobs/9", "Lead Java developer team", "Java,Spring Boot,Leadership", "LINKEDIN", $true),
    @("Java DevOps Engineer", "Mindtree", "Bangalore", "https://linkedin.com/jobs/10", "Java DevOps CI/CD", "Java,DevOps,CI/CD,Docker", "LINKEDIN", $true)
)

# Add-JobsBatch -JobList $jobsBatch

Write-Host @"

========================================
  Bulk Job Import Script Loaded!
========================================

Commands available:

1. Import from CSV:
   Import-JobsFromCSV -CsvPath "jobs.csv"

2. Add batch from array:
   Add-JobsBatch -JobList `$jobsBatch

========================================
"@ -ForegroundColor Green
