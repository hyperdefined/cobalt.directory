<footer class="rainbow-top">
	<p>
		This site is not official or related to <a href="https://imput.net/">imput.net</a> (the creators of
		cobalt).
	</p>
	<p>
		This site was build with love, care, and fluff! Created and managed by <a
			href="https://hyper.lol">hyperdefined</a
		>.
	</p>
	<p>
		This site and project is licensed under <a
			href="https://github.com/hyperdefined/cobalt.directory/blob/master/LICENSE">MIT</a
		>.
	</p>
</footer>

<style>
	/* This footer was taken from https://github.com/xz/new.css/pull/72 */
	footer {
		background: #111111;
		padding: 2rem 1.5rem;
		margin: 2rem calc(0px - (50vw - 50%)) -2rem;
		padding-left: calc(50vw - 50%);
		padding-right: calc(50vw - 50%);
	}
	footer > *:first-child {
		margin-bottom: 0;
		padding-bottom: 0;
	}

	footer > *:last-child {
		margin-top: 0;
	}

	.rainbow-top {
		position: relative;
	}

	.rainbow-top::after {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
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

	@media (prefers-color-scheme: light) {
		footer {
			background-color: #f6f8fa !important;
		}
	}
</style>
