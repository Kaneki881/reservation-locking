$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($maven) {
    $mavenPath = $maven.Source
} else {
    $mavenPath = 'C:\Program Files\maven-mvnd-1.0.3-windows-amd64\mvn\bin\mvn.cmd'
    if (-not (Test-Path -LiteralPath $mavenPath)) {
        Write-Error 'Maven est introuvable. Installez Maven ou ajoutez mvn à PATH.'
        exit 1
    }
}

chcp 65001 > $null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

Push-Location $PSScriptRoot
try {
    & $mavenPath test -q
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
