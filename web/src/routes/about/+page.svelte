<script lang="ts">
	import { page } from '$app/state';
	const currentUrl = page.url.href;
</script>

<svelte:head>
	<title>About</title>
	<meta property="og:url" content={currentUrl} />
	<meta property="og:title" content="About" />
	<meta property="og:description" content="Information and history behind cobalt.directory." />
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content="About" />
	<meta name="twitter:description" content="Information and history behind cobalt.directory." />
</svelte:head>

<h2>About</h2>
<p>
	In August of 2023, I started to track a list of cobalt instances for fun, called
	<code>instances.hyper.lol</code>. At the start, this list was public and had to be user submitted. This list
	was from the cobalt Discord server.
</p>

<p>
	Eventually, I found out ways to search the internet for public cobalt instances. I wrote a blog
	post about it <a href="https://hyper.lol/blog/4">here</a>.
</p>

<p>
	However, this list ended up growing larger than expected, and eventually was used for abuse.
	Different "social media downloaders" websites were taking my list and using the cobalt instances
	as their backend. They were profiting from open-source software. Furthermore, most instances were
	not aware of this list, and didn't consent to being on the list.
</p>

<p>
	I decided to close down the site at the end of 2024. Around that time, <a
		href="https://instances.cobalt.best">another instance tracker</a
	> was created, but only with instances that were asked to be added.
</p>

<p>
	However, I continued to work on the list privately. I still would search the internet for cobalt
	instances and track them. I decided my list was done for the time, as the other tracker was much
	better.
</p>

<p>
	Eventually in July 2025, I decided to make this site public again, but only with instances that
	had permission to be added. The private list still exists, but will not be public again.
</p>
