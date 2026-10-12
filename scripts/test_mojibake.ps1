# Test recovering Windows-1252 mojibake to UTF-8
$str = "## ðŸš€ 100+ Detailed Domain Scenarios & Enterprise Lab"
$enc1252 = [System.Text.Encoding]::GetEncoding("windows-1252")
$encUtf8 = [System.Text.Encoding]::UTF8
$bytes = $enc1252.GetBytes($str)
$recovered = $encUtf8.GetString($bytes)
[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "Original:  $str"
Write-Host "Recovered: $recovered"
