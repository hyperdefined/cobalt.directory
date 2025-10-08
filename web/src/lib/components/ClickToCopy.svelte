<script lang="ts">
	export let text = '';

	let showTip = false;
	let tip = 'Click to copy';
	let resetTimer: number | null = null;

	async function copy() {
		try {
			await navigator.clipboard.writeText(text);
			tip = 'Copied!';
		} catch {
			tip = 'Press Ctrl+C to copy';
		}
		showTip = true;
		if (resetTimer) clearTimeout(resetTimer);
		resetTimer = window.setTimeout(() => {
			tip = 'Click to copy';
			showTip = false;
			resetTimer = null;
		}, 1400);
	}
</script>

<button
	class="copy"
	on:click={copy}
	on:mouseenter={() => (showTip = true)}
	on:mouseleave={() => (showTip = false)}
	aria-label="Copy to clipboard"
>
	<code>{text}</code>
	<span class="tooltip" class:visible={showTip} role="status" aria-live="polite">{tip}</span>
</button>

<style>
	.copy {
		position: relative;
		border: none;
		background: transparent;
		padding: 0;
		cursor: pointer;
	}
	.copy code {
		display: inline-block;
		padding: 0.25rem 0.5rem;
		border-radius: 6px;
		background: rgba(127, 127, 127, 0.15);
	}
	.tooltip {
		position: absolute;
		left: 50%;
		bottom: calc(100% + 8px);
		transform: translateX(-50%) translateY(4px);
		padding: 4px 8px;
		font-size: 0.8rem;
		white-space: nowrap;
		background: #000;
		color: #fff;
		border-radius: 6px;
		opacity: 0;
		pointer-events: none;
		transition:
			opacity 120ms ease,
			transform 120ms ease;
		box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
	}
	.tooltip::after {
		content: '';
		position: absolute;
		top: 100%;
		left: 50%;
		margin-left: -6px;
		border: 6px solid transparent;
		border-top-color: #000;
	}
	.tooltip.visible {
		opacity: 1;
		transform: translateX(-50%) translateY(0);
	}
</style>
