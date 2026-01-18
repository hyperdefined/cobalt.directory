<script lang="ts">
	import { onMount } from 'svelte';

	let visible = false;

	const STORAGE_KEY = 'closed-donate-popup';

	function close() {
		visible = false;
		localStorage.setItem(STORAGE_KEY, '1');
	}

	onMount(() => {
		// already dismissed, do nothing
		if (localStorage.getItem(STORAGE_KEY)) return;

		setTimeout(() => {
			visible = true;
		}, 3000);
	});
</script>

{#if visible}
	<div class="donate-toast" role="dialog" aria-live="polite">
		<button class="close" on:click={close} aria-label="Dismiss">x</button>

		<strong>Like cobalt.directory?</strong>
		<a
			href="https://buymeacoffee.com/hyperdefined"
			class="donate-btn"
			on:click={close}
		>
			Support the project
		</a>
	</div>
{/if}

<style>
	.donate-toast {
		position: fixed;
		bottom: 1.25rem;
		right: 1.25rem;
		max-width: 260px;

		background: var(--card-bg, #111);
		color: var(--text, #fff);
		border: 1px solid rgba(255,255,255,0.08);
		border-radius: 10px;
		padding: 0.9rem 1rem;

		box-shadow: 0 10px 30px rgba(0,0,0,0.35);
		z-index: 1000;
		font-size: 0.9rem;
	}

	.donate-btn {
		display: inline-block;
		margin-top: 0.25rem;
		padding: 0.4rem 0.7rem;
		border-radius: 6px;
		background: #ff4d8d;
		color: #fff;
		text-decoration: none;
		font-weight: 500;
	}

	.donate-btn:hover {
		filter: brightness(1.1);
	}

	.close {
		position: absolute;
		top: 4px;
		right: 6px;
		background: none;
		border: none;
		color: inherit;
		font-size: 1.1rem;
		cursor: pointer;
		opacity: 0.6;
	}

	.close:hover {
		opacity: 1;
	}
</style>