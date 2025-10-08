declare module '*.md' {
    import type { SvelteComponentDev } from 'svelte/internal';

    export default class Comp extends SvelteComponentDev {
        $$prop_def: {};
    }
    export const metadata: Record<string, any>;
}

// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

export {};
