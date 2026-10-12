$path = "java-core/java_collection_stream.md"
$line = (Get-Content $path -Encoding UTF8)[416]
$enc1252 = [System.Text.Encoding]::GetEncoding("windows-1252")
$encUtf8 = [System.Text.Encoding]::UTF8

$bytes = $enc1252.GetBytes($line)
$recovered = $encUtf8.GetString($bytes)

[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Output "Line 417 raw:       $line"
Write-Output "Line 417 recovered: $recovered"
