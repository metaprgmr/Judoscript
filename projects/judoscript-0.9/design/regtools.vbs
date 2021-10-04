'Enable/Disable Registry Editing tools
'© Doug Knox - rev 12/06/99

Option Explicit

'Declare variables
Dim WSHShell, n, MyBox, p, t, mustboot, errnum, vers
Dim enab, disab, jobfunc, itemtype

Set WSHShell = WScript.CreateObject("WScript.Shell")
p = "HKCU\Software\Microsoft\Windows\CurrentVersion\Policies\System\"
p = p & "DisableRegistryTools"
itemtype = "REG_DWORD"
mustboot = "Log off and back on, or restart your pc to" & vbCR & "effect the changes"
enab = "ENABLED"
disab = "DISABLED"
jobfunc = "Registry Editing Tools are now "

'This section tries to read the registry key value. If not present an 
'error is generated.  Normal error return should be 0 if value is 
'present
t = "Confirmation"
Err.Clear
On Error Resume Next
n = WSHShell.RegRead (p)
On Error Goto 0
errnum = Err.Number

if errnum <> 0 then
'Create the registry key value for DisableRegistryTools with value 0
	WSHShell.RegWrite p, 0, itemtype
End If

'If the key is present, or was created, it is toggled
'Confirmations can be disabled by commenting out 
'the two MyBox lines below

If n = 0 Then
	n = 1
WSHShell.RegWrite p, n, itemtype
Mybox = MsgBox(jobfunc & disab & vbCR & mustboot, 4096, t)
ElseIf n = 1 then
	n = 0
WSHShell.RegWrite p, n, itemtype
Mybox = MsgBox(jobfunc & enab & vbCR & mustboot, 4096, t)
End If
