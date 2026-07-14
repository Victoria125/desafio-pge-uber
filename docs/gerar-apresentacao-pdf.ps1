param(
    [string]$SourcePath = (Join-Path $PSScriptRoot 'apresentacao-projeto.md'),
    [string]$OutputPath = (Join-Path $PSScriptRoot 'apresentacao-projeto.pdf')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Wrap-Text {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [Parameter(Mandatory = $true)][int]$MaxChars
    )

    if ($Text.Length -le $MaxChars) {
        return @($Text)
    }

    $prefix = ''
    $continuationPrefix = ''
    if ($Text -match '^(\s*[-]\s+|\s*\d+\.\s+)(.*)$') {
        $prefix = $Matches[1]
        $continuationPrefix = ' ' * $prefix.Length
        $Text = $Matches[2]
    }

    $words = $Text -split '\s+'
    $lines = New-Object System.Collections.Generic.List[string]
    $current = $prefix
    $currentLimit = $MaxChars

    foreach ($word in $words) {
        if ([string]::IsNullOrWhiteSpace($word)) {
            continue
        }

        if (($current.Trim()).Length -eq 0 -or $current -eq $prefix -or $current -eq $continuationPrefix) {
            $candidate = "$current$word"
        } else {
            $candidate = "$current $word"
        }

        if ($candidate.Length -le $currentLimit) {
            $current = $candidate
            continue
        }

        if (($current.Trim()).Length -gt 0) {
            $lines.Add($current)
        }

        if ($word.Length -gt ($MaxChars - $continuationPrefix.Length)) {
            $remaining = $word
            while ($remaining.Length -gt ($MaxChars - $continuationPrefix.Length)) {
                $chunkSize = $MaxChars - $continuationPrefix.Length
                $lines.Add($continuationPrefix + $remaining.Substring(0, $chunkSize))
                $remaining = $remaining.Substring($chunkSize)
            }
            $current = $continuationPrefix + $remaining
        } else {
            $current = $continuationPrefix + $word
        }
    }

    if (($current.Trim()).Length -gt 0) {
        $lines.Add($current)
    }

    return $lines.ToArray()
}

function Add-LayoutLine {
    param(
        [System.Collections.Generic.List[object]]$Items,
        [string]$Text,
        [string]$Font,
        [int]$Size,
        [int]$Leading,
        [int]$Before = 0,
        [int]$After = 0
    )

    $Items.Add([pscustomobject]@{
        Type = 'line'
        Text = $Text
        Font = $Font
        Size = $Size
        Leading = $Leading
        Before = $Before
        After = $After
    })
}

function ConvertTo-PdfHexString {
    param([string]$Text)

    $encoding = [System.Text.Encoding]::GetEncoding(1252)
    $bytes = $encoding.GetBytes($Text)
    $parts = foreach ($b in $bytes) { $b.ToString('X2') }
    return ($parts -join '')
}

function Add-AsciiBytes {
    param(
        [System.Collections.Generic.List[byte]]$Buffer,
        [string]$Text
    )

    $bytes = [System.Text.Encoding]::ASCII.GetBytes($Text)
    $Buffer.AddRange($bytes)
}

if (-not (Test-Path -LiteralPath $SourcePath)) {
    throw "Arquivo fonte nao encontrado: $SourcePath"
}

$markdown = Get-Content -LiteralPath $SourcePath -Raw -Encoding UTF8
$rawLines = $markdown -split "`r?`n"
$items = New-Object System.Collections.Generic.List[object]
$inCode = $false

foreach ($raw in $rawLines) {
    $line = $raw.TrimEnd()

    if ($line -eq '[PAGEBREAK]') {
        $items.Add([pscustomobject]@{ Type = 'pagebreak' })
        continue
    }

    if ($line -match '^```') {
        $inCode = -not $inCode
        continue
    }

    if ($line.Trim().Length -eq 0) {
        $items.Add([pscustomobject]@{ Type = 'space'; Height = 7 })
        continue
    }

    if ($inCode) {
        foreach ($wrapped in (Wrap-Text -Text ('    ' + $line) -MaxChars 82)) {
            Add-LayoutLine -Items $items -Text $wrapped -Font 'F3' -Size 8 -Leading 11
        }
        continue
    }

    if ($line -match '^#\s+(.+)$') {
        foreach ($wrapped in (Wrap-Text -Text $Matches[1] -MaxChars 58)) {
            Add-LayoutLine -Items $items -Text $wrapped -Font 'F2' -Size 18 -Leading 25 -Before 4 -After 6
        }
        continue
    }

    if ($line -match '^##\s+(.+)$') {
        foreach ($wrapped in (Wrap-Text -Text $Matches[1] -MaxChars 72)) {
            Add-LayoutLine -Items $items -Text $wrapped -Font 'F2' -Size 14 -Leading 20 -Before 6 -After 4
        }
        continue
    }

    if ($line -match '^###\s+(.+)$') {
        foreach ($wrapped in (Wrap-Text -Text $Matches[1] -MaxChars 78)) {
            Add-LayoutLine -Items $items -Text $wrapped -Font 'F2' -Size 12 -Leading 17 -Before 4 -After 3
        }
        continue
    }

    foreach ($wrapped in (Wrap-Text -Text $line -MaxChars 92)) {
        Add-LayoutLine -Items $items -Text $wrapped -Font 'F1' -Size 10 -Leading 14
    }
}

$pageWidth = 595
$pageHeight = 842
$marginLeft = 50
$top = 790
$bottom = 50

$pages = New-Object System.Collections.Generic.List[object]
$currentPage = New-Object System.Collections.Generic.List[object]
$y = $top

function Start-NewPage {
    param(
        [System.Collections.Generic.List[object]]$Pages,
        [ref]$CurrentPage,
        [ref]$CurrentY
    )

    if ($CurrentPage.Value.Count -gt 0) {
        $Pages.Add($CurrentPage.Value.ToArray())
    }
    $CurrentPage.Value = New-Object System.Collections.Generic.List[object]
    $CurrentY.Value = $top
}

foreach ($item in $items) {
    if ($item.Type -eq 'pagebreak') {
        Start-NewPage -Pages $pages -CurrentPage ([ref]$currentPage) -CurrentY ([ref]$y)
        continue
    }

    if ($item.Type -eq 'space') {
        if (($y - $item.Height) -lt $bottom) {
            Start-NewPage -Pages $pages -CurrentPage ([ref]$currentPage) -CurrentY ([ref]$y)
        } else {
            $y -= $item.Height
        }
        continue
    }

    $needed = $item.Before + $item.Leading + $item.After
    if (($y - $needed) -lt $bottom) {
        Start-NewPage -Pages $pages -CurrentPage ([ref]$currentPage) -CurrentY ([ref]$y)
    }

    $y -= $item.Before
    $currentPage.Add([pscustomobject]@{
        Text = $item.Text
        Font = $item.Font
        Size = $item.Size
        X = $marginLeft
        Y = $y
    })
    $y -= ($item.Leading + $item.After)
}

if ($currentPage.Count -gt 0) {
    $pages.Add($currentPage.ToArray())
}

if ($pages.Count -lt 10) {
    throw "O PDF teria apenas $($pages.Count) paginas. Adicione mais conteudo ao Markdown."
}

$objects = @{}
$pageRefs = New-Object System.Collections.Generic.List[string]
$maxObjectId = 5 + ($pages.Count * 2)

$objects[1] = "1 0 obj`n<< /Type /Catalog /Pages 2 0 R >>`nendobj`n"
$objects[3] = "3 0 obj`n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>`nendobj`n"
$objects[4] = "4 0 obj`n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>`nendobj`n"
$objects[5] = "5 0 obj`n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>`nendobj`n"

for ($i = 0; $i -lt $pages.Count; $i++) {
    $pageObjectId = 6 + ($i * 2)
    $contentObjectId = $pageObjectId + 1
    $pageRefs.Add("$pageObjectId 0 R")

    $commands = New-Object System.Text.StringBuilder
    foreach ($line in $pages[$i]) {
        $hex = ConvertTo-PdfHexString -Text $line.Text
        [void]$commands.Append("BT /$($line.Font) $($line.Size) Tf 1 0 0 1 $($line.X) $($line.Y) Tm <$hex> Tj ET`n")
    }

    $footerText = "Pagina $($i + 1) de $($pages.Count) - Ride Challenge"
    $footerHex = ConvertTo-PdfHexString -Text $footerText
    [void]$commands.Append("BT /F1 8 Tf 1 0 0 1 50 28 Tm <$footerHex> Tj ET`n")

    $stream = $commands.ToString()
    $streamLength = [System.Text.Encoding]::ASCII.GetByteCount($stream)

    $objects[$pageObjectId] = "$pageObjectId 0 obj`n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] /Resources << /Font << /F1 3 0 R /F2 4 0 R /F3 5 0 R >> >> /Contents $contentObjectId 0 R >>`nendobj`n"
    $objects[$contentObjectId] = "$contentObjectId 0 obj`n<< /Length $streamLength >>`nstream`n$stream`nendstream`nendobj`n"
}

$objects[2] = "2 0 obj`n<< /Type /Pages /Kids [ $($pageRefs -join ' ') ] /Count $($pages.Count) >>`nendobj`n"

$buffer = New-Object System.Collections.Generic.List[byte]
Add-AsciiBytes -Buffer $buffer -Text "%PDF-1.4`n% Generated by local PowerShell PDF writer`n"

$offsets = New-Object System.Collections.Generic.List[int]
$offsets.Add(0)

for ($id = 1; $id -le $maxObjectId; $id++) {
    if (-not $objects.ContainsKey($id)) {
        throw "Objeto PDF ausente: $id"
    }
    $offsets.Add($buffer.Count)
    Add-AsciiBytes -Buffer $buffer -Text $objects[$id]
}

$xrefOffset = $buffer.Count
Add-AsciiBytes -Buffer $buffer -Text "xref`n0 $($maxObjectId + 1)`n"
Add-AsciiBytes -Buffer $buffer -Text "0000000000 65535 f `n"

for ($id = 1; $id -le $maxObjectId; $id++) {
    Add-AsciiBytes -Buffer $buffer -Text ("{0:D10} 00000 n `n" -f $offsets[$id])
}

Add-AsciiBytes -Buffer $buffer -Text "trailer`n<< /Size $($maxObjectId + 1) /Root 1 0 R >>`nstartxref`n$xrefOffset`n%%EOF`n"

[System.IO.Directory]::CreateDirectory((Split-Path -Parent $OutputPath)) | Out-Null
[System.IO.File]::WriteAllBytes($OutputPath, $buffer.ToArray())

Write-Output "PDF gerado: $OutputPath"
Write-Output "Paginas: $($pages.Count)"
