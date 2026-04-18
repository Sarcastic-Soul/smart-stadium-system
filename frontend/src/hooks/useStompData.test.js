import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useStompData } from "./useStompData";
import { Client } from "@stomp/stompjs";

vi.mock("@stomp/stompjs", () => ({
    Client: vi.fn().mockImplementation(() => ({
        activate: vi.fn(),
        deactivate: vi.fn(),
        subscribe: vi.fn(),
    })),
}));

describe("useStompData", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("fetches initial data and establishes STOMP connection", async () => {
        const mockInitialData = { crowdDensities: [], queueWaitTimes: [] };
        const mockFetch = vi.fn().mockResolvedValue(mockInitialData);

        let onConnectCallback;
        Client.mockImplementationOnce(() => ({
            activate: vi.fn(),
            deactivate: vi.fn(),
            subscribe: vi.fn(),
            set onConnect(cb) {
                onConnectCallback = cb;
            },
        }));

        const { result } = renderHook(() => useStompData(mockFetch));

        expect(result.current.loading).toBe(true);

        // Wait for the async fetch to finish
        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(result.current.data).toEqual(mockInitialData);
        expect(mockFetch).toHaveBeenCalled();

        // simulate connect
        if (onConnectCallback) {
            act(() => {
                onConnectCallback();
            });
        }
    });
});
