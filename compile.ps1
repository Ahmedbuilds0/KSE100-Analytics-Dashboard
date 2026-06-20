if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
Write-Host "Compiling JavaFX KSE-100 Comparative Analytics Dashboard..." -ForegroundColor Cyan
javac --module-path "C:\Users\user\Downloads\openjfx-21.0.11_windows-x64_bin-sdk\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml -d bin src/com/kse100/Main.java src/com/kse100/model/*.java src/com/kse100/service/*.java src/com/kse100/ui/*.java
if ($LASTEXITCODE -eq 0) {
    Copy-Item "resources/style.css" "bin/style.css" -Force
    Write-Host "Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed with exit code $LASTEXITCODE" -ForegroundColor Red
}
