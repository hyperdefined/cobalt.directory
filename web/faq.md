---
permalink: /faq/
title: "FAQ"
description: "Questions and answers for cobalt.directory."
---
### General
<details>
<summary>What is cobalt?</summary>
cobalt is an open-source media downloader. It supports a wide range of social media websites. No ads, tracking, or paywalls. It was created by <a href="https://github.com/imputnet/">imput</a>.
</details>

<details>
<summary>What is an "instance"?</summary>
An instance is simply another "copy" of cobalt. Because cobalt is open source, anyone can start up their own instance. Each entry on the tracker is an instance of cobalt.
</details>

<details>
<summary>What is this tracker used for?</summary>
This site is used to track these instances. It uses a score system to determine which community instance (and official one) are the best. It allows users to use other instances if the official one goes offline or has issues.
<br><br>
The official instance sees <i>a lot</i> of traffic, so some services may be blocked. Using other instances until the official is fixed is the idea.
</details>

<details>
<summary>What is the difference between official and community instances?</summary>
Official instance is the main cobalt instance by the developers. This instance is <code>cobalt.tools</code>, and the API is <code>api.cobalt.tools</code>. All others on this list are community hosted and might have their own quirks.
</details>

<details>
<summary>What is the difference between API and frontend?</summary>
The frontend is the web app you see when you visit a cobalt instance. The API is another module that handles any download requests sent by the frontend. It does the processing and handling. When you enter a URL and download it, the frontend sends a request to the API, and it returns the media back.
</details>

### Instance List
<details>
<summary>How do I read the instance list?</summary>
There's a few ways to see the instances, by the main list or by service.

<ul>
<li><a href="{{ site.url }}">Instance list</a>: see all cobalt instances.</li>
<li><a href="{{ site.url }}/service/">By service</a>: see what services work on what instances.</li>
</ul>

To use an instance, simply click the link under the instance column. Or, add their API URL (found on the instance page, click the score) <a href="https://cobalt.tools/settings/instances#community">here</a>.
</details>

<details>
<summary>Why are some different colors on the list?</summary>
Colors are based on the score. The higher score of the instance, the more green it appears. The lower the score, the more red it appears.
</details>

<details>
<summary>I want to add/remove my instance!</summary>
If you want to be added/removed, ping @hyperdefined on the <a href="https://discord.gg/pQPt8HBUPu">cobalt discord</a> or create a pull request <a href="https://github.com/hyperdefined/cobalt.directory">here</a>.
</details>