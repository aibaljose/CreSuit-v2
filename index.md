<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>CreSuit Documentation</title>

<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@300;400;500;700&display=swap" rel="stylesheet">

<style>
:root{
    --bg:#0b0b0b;
    --surface:#111111;
    --surface2:#181818;
    --border:#2d2d2d;
    --text:#ffffff;
    --muted:#8a8a8a;
    --accent:#ffffff;
}

*{
    margin:0;
    padding:0;
    box-sizing:border-box;
}

html,body{
    width:100%;
    height:100%;
    background:var(--bg);
    color:var(--text);
    font-family:"IBM Plex Mono",monospace;
}

body{
    display:flex;
    flex-direction:column;
}

/* ---------- Header ---------- */

header{
    height:72px;
    display:flex;
    align-items:center;
    justify-content:space-between;
    padding:0 32px;
    border-bottom:1px solid var(--border);
    background:rgba(10,10,10,.92);
    backdrop-filter:blur(16px);
}

.logo{
    display:flex;
    align-items:center;
    gap:14px;
}

.dot{
    width:12px;
    height:12px;
    border-radius:50%;
    background:#fff;
    box-shadow:0 0 12px #fff;
}

.logo h1{
    font-size:20px;
    font-weight:700;
    letter-spacing:4px;
    text-transform:uppercase;
}

.subtitle{
    color:var(--muted);
    font-size:12px;
    letter-spacing:2px;
    text-transform:uppercase;
}

/* ---------- Button ---------- */

.btn{
    color:#fff;
    border:1px solid var(--border);
    text-decoration:none;
    padding:10px 18px;
    border-radius:999px;
    transition:.25s;
    font-size:12px;
    letter-spacing:1px;
}

.btn:hover{
    background:#fff;
    color:#000;
}

/* ---------- Frame ---------- */

.viewer{
    flex:1;
    padding:24px;
    background:#0b0b0b;
}

.frame{
    width:100%;
    height:100%;
    border:1px solid var(--border);
    border-radius:28px;
    overflow:hidden;
    background:#fff;
    box-shadow:
    inset 0 0 0 1px rgba(255,255,255,.03),
    0 25px 60px rgba(0,0,0,.45);
}

iframe{
    width:100%;
    height:100%;
    border:none;
}

/* ---------- Footer ---------- */

footer{
    height:48px;
    border-top:1px solid var(--border);
    display:flex;
    justify-content:space-between;
    align-items:center;
    padding:0 24px;
    font-size:11px;
    color:var(--muted);
    letter-spacing:1px;
}

@media(max-width:700px){

header{
    padding:18px;
}

.logo h1{
    font-size:16px;
}

.subtitle{
    display:none;
}

.btn{
    display:none;
}

.viewer{
    padding:10px;
}

.frame{
    border-radius:18px;
}

}
</style>

</head>
<body>

<header>

<div class="logo">
<div class="dot"></div>

<div>
<h1>CreSuit</h1>
<div class="subtitle">
AI No-Code Platform Documentation
</div>
</div>
</div>

<a class="btn"
href="https://docs.google.com/document/d/e/2PACX-1vSgFbDcZl5uFvw0WfjCdjQlC8iJUtw4ozJsA1z5aGYis4mQPrvyEOswXjQnM5K65hlvmMePl2hQyg67/pub"
target="_blank">
OPEN DOCUMENT
</a>

</header>

<div class="viewer">

<div class="frame">

<iframe
src="https://docs.google.com/document/d/e/2PACX-1vSgFbDcZl5uFvw0WfjCdjQlC8iJUtw4ozJsA1z5aGYis4mQPrvyEOswXjQnM5K65hlvmMePl2hQyg67/pub?embedded=true">
</iframe>

</div>

</div>

<footer>

<div>CRESUIT • DOCUMENTATION</div>

<div>NOTHING INSPIRED UI • 2026</div>

</footer>

</body>
</html>
