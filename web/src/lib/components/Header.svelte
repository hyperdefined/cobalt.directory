<script lang="ts">
	import { afterNavigate } from '$app/navigation';
	let menuOpen = false;
	afterNavigate(() => (menuOpen = false));
</script>

<header class="rainbow-bottom">
	<div class="navbar-icon">
		<img
			src="https://raw.githubusercontent.com/imputnet/cobalt/main/web/static/icons/generic.png"
			alt="cobalt icon"
		/>
	</div>
	<h1>cobalt.directory</h1>
	<button
		class="menu-toggle"
		aria-label="Toggle navigation"
		aria-expanded={menuOpen}
		aria-controls="site-nav"
		on:click={() => (menuOpen = !menuOpen)}
	>
		☰
	</button>
	<nav id="site-nav" class="nav-menu" class:active={menuOpen}>
		<ul>
			<li><a href="/">Instances</a></li>
			<li><a href="/service" data-sveltekit-preload-data>Search By Service</a></li>
			<li><a href="/about">About</a></li>
			<li><a href="/faq">FAQ</a></li>
			<li><a href="https://github.com/hyperdefined/cobalt.directory">Source</a></li>
		</ul>
	</nav>
</header>

<style>
	header {
		display: flex;
		flex-direction: column;
		align-items: center;
		position: relative;
	}
	.rainbow-bottom {
		position: relative;
		background: rgb(25, 25, 25);
	}

	.rainbow-bottom::after {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		height: 3px;
		pointer-events: none;
		z-index: 1;

		background: linear-gradient(90deg, red, orange, yellow, green, blue, indigo, violet, red);
		background-size: 200% 100%;
		animation: rainbow-slide 8s linear infinite;
	}

	@keyframes rainbow-slide {
		to {
			background-position: 200% 0;
		}
	}

	.navbar-icon {
		width: 155px;
		height: 155px;
		overflow: hidden;
		border-radius: 50%;
	}

	nav {
		width: 100%;
		margin-top: 10px;
	}
	nav ul {
		list-style-type: none;
		margin: 0;
		padding: 0;
		overflow: hidden;
		display: flex;
		justify-content: center;
	}
	nav li {
		margin: 0;
	}
	nav li a {
		display: block;
		color: white;
		text-align: center;
		padding: 14px 16px;
		text-decoration: none;
		white-space: nowrap;
		background-color: unset;
	}
	nav li a:hover {
		background-color: #555;
		color: white;
	}
	nav li a:active {
		background-color: #555;
		color: white;
	}
	nav li a:focus {
		background-color: #444;
		color: white;
	}

	.menu-toggle {
		display: none;
		background-color: #555;
		color: white;
		border: none;
		padding: 10px;
		cursor: pointer;
		font-size: 15px;
		-webkit-tap-highlight-color: transparent;
	}
	.menu-toggle:active,
	.menu-toggle:hover {
		background-color: #555;
	}
	.menu-toggle:focus {
		background-color: #444;
		box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5);
	}

	@media screen and (max-width: 768px) {
		nav ul {
			flex-direction: column;
			align-items: center;
			display: none;
		}
		.menu-toggle {
			display: block;
		}
		nav.active ul {
			display: flex;
		}
		nav li {
			width: 100%;
		}
		nav li a {
			white-space: normal;
		}
	}

	@media (prefers-color-scheme: light) {
		nav li {
			background-color: rgb(225, 225, 225);
		}
		nav li a {
			color: black;
		}
		.menu-toggle {
			color: black;
			background-color: rgb(225, 225, 225);
		}
		header {
			background-color: #f6f8fa !important;
		}
	}
</style>
