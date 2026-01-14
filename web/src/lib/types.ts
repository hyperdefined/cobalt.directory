export type ServiceResult = {
	friendly?: string;
	message: string;
	status: boolean;
};

export type Instance = {
	protocol: string;
	tests: Record<string, ServiceResult>;
	api: string;
	frontend: string;
	version?: string;
	online?: boolean;
	remote?: string;
	official?: boolean;
	startTime?: number;
};
