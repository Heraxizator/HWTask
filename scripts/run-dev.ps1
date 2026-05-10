# Spring Boot + Postgres. Swagger: http://localhost:8080/swagger-ui.html
$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
Set-Location $Root

function Use-JetBrainsJbrIfNeeded {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    $needsJdk = $true
    if ($javaCmd) {
        # java -version writes to stderr; avoid terminating on stderr when ErrorActionPreference is Stop
        $prevEa = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        $verOut = (& java -version 2>&1 | Out-String)
        $ErrorActionPreference = $prevEa
        if ($verOut -match 'version "1\.(\d+)\.') { $maj = [int]$Matches[1] }
        elseif ($verOut -match 'version "(\d+)"') { $maj = [int]$Matches[1] }
        else { $maj = 0 }
        $needsJdk = ($maj -lt 17)
    }
    if (-not $needsJdk) { return }

    $ideaRoots = @(
        "C:\Program Files\JetBrains",
        "${env:ProgramFiles}\JetBrains",
        "${env:ProgramFiles(x86)}\JetBrains"
    ) | Where-Object { $_ -and (Test-Path $_) }

    foreach ($base in $ideaRoots) {
        $ideaDir = Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like "IntelliJ IDEA*" } |
            Sort-Object Name -Descending |
            Select-Object -First 1
        if (-not $ideaDir) { continue }
        $jbr = Join-Path $ideaDir.FullName "jbr"
        $javac = Join-Path $jbr "bin\javac.exe"
        if (Test-Path $javac) {
            $env:JAVA_HOME = $jbr
            $env:Path = "$(Join-Path $jbr 'bin');$env:Path"
            Write-Host "Using IntelliJ JBR: JAVA_HOME=$jbr" -ForegroundColor DarkGray
            return
        }
    }
    Write-Host "ERROR: JDK 17+ required. Install JDK or IntelliJ IDEA (bundled JBR)." -ForegroundColor Red
    exit 1
}

Use-JetBrainsJbrIfNeeded

Write-Host "Starting PostgreSQL (docker compose)..." -ForegroundColor Cyan
try {
    docker compose up -d 2>&1 | Out-Host
    if ($LASTEXITCODE -ne 0) { throw "exit $LASTEXITCODE" }
} catch {
    Write-Warning "docker compose failed - start Docker Desktop, then: docker compose up -d"
}

Write-Host "Starting Spring Boot (profiles: dev,no-compose)..." -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Green

& (Join-Path $Root "mvnw.cmd") @(
    "spring-boot:run",
    "-Dspring-boot.run.arguments=--spring.profiles.active=dev,no-compose"
)
