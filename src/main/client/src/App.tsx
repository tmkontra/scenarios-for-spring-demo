import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Env } from "./Env";

type AppProps = {
  initialView?: "home" | "report" | "missing";
};

type LoadState =
  | { status: "idle"; value: string }
  | { status: "loading"; value: string }
  | { status: "loaded"; value: string }
  | { status: "error"; value: string };

type ScenarioOverrideQuery = {
  endpointAlias: string;
  scenarioId: string;
};

const scenarioOverrideQueryParam = "scenarioOverride";

const parseScenarioOverrideQuery = (
  value: string,
): ScenarioOverrideQuery | null => {
  const separatorIndex = value.indexOf(":");
  if (separatorIndex < 1 || separatorIndex === value.length - 1) {
    return null;
  }

  const endpointAlias = value.slice(0, separatorIndex).trim();
  const scenarioId = value.slice(separatorIndex + 1).trim();
  if (!endpointAlias || !scenarioId) {
    return null;
  }

  return { endpointAlias, scenarioId };
};

const isScenarioOverrideQuery = (
  value: ScenarioOverrideQuery | null,
): value is ScenarioOverrideQuery => value !== null;

function App({ initialView = "home" }: AppProps) {
  const reportEndpointAlias = "my-report";
  const [searchParams, setSearchParams] = useSearchParams();
  const [user, setUser] = useState<LoadState>({
    status: "idle",
    value: "No user loaded yet.",
  });
  const [report, setReport] = useState<LoadState>({
    status: "idle",
    value: "No report loaded yet.",
  });

  const authorizationHeader = searchParams.get("authorization") ?? "";
  const reportScenarioId =
    searchParams
      .getAll(scenarioOverrideQueryParam)
      .map(parseScenarioOverrideQuery)
      .filter(isScenarioOverrideQuery)
      .find((override) => override.endpointAlias === reportEndpointAlias)
      ?.scenarioId ?? "";

  const updateQueryParam = (name: string, value: string) => {
    setSearchParams((currentParams) => {
      const nextParams = new URLSearchParams(currentParams);
      if (value.trim()) {
        nextParams.set(name, value);
      } else {
        nextParams.delete(name);
      }
      return nextParams;
    });
  };

  const updateScenarioOverride = (endpointAlias: string, scenarioId: string) => {
    setSearchParams((currentParams) => {
      const nextParams = new URLSearchParams(currentParams);
      const existingOverrides = nextParams
        .getAll(scenarioOverrideQueryParam)
        .filter((value) => {
          const override = parseScenarioOverrideQuery(value);
          return override?.endpointAlias !== endpointAlias;
        });

      nextParams.delete(scenarioOverrideQueryParam);
      existingOverrides.forEach((value) =>
        nextParams.append(scenarioOverrideQueryParam, value),
      );

      if (scenarioId.trim()) {
        nextParams.append(
          scenarioOverrideQueryParam,
          `${endpointAlias}:${scenarioId.trim()}`,
        );
      }

      return nextParams;
    });
  };

  const apiHeaders = () => {
    const headers: Record<string, string> = {};
    const authorization = searchParams.get("authorization") ?? "";
    const scenarioOverrides = searchParams
      .getAll(scenarioOverrideQueryParam)
      .map(parseScenarioOverrideQuery)
      .filter(isScenarioOverrideQuery)
      .map((override) => `${override.endpointAlias}=${override.scenarioId}`)
      .join(",");

    if (authorization) {
      headers.Authorization = authorization;
    }
    if (scenarioOverrides) {
      headers["X-Scenario-Override"] = scenarioOverrides;
    }

    return headers;
  };

  const fetchUser = () => {
    setUser({ status: "loading", value: "Loading user..." });
    fetch(`${Env.API_BASE_URL}/user`, {
      headers: apiHeaders(),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Request failed with ${response.status}`);
        }
        return response.json();
      })
      .then((value) =>
        setUser({ status: "loaded", value: JSON.stringify(value, null, 2) }),
      )
      .catch((error: Error) =>
        setUser({ status: "error", value: error.message }),
      );
  };

  const fetchReport = () => {
    setReport({ status: "loading", value: "Loading report..." });
    fetch(`${Env.API_BASE_URL}/my-report`, {
      headers: apiHeaders(),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Request failed with ${response.status}`);
        }
        return response.text();
      })
      .then((value) => setReport({ status: "loaded", value }))
      .catch((error: Error) =>
        setReport({ status: "error", value: error.message }),
      );
  };

  useEffect(() => {
    if (initialView === "report") {
      fetchReport();
    }
  }, [initialView]);

  if (initialView === "missing") {
    return (
      <main className="app-shell">
        <section className="panel">
          <p className="eyebrow">404</p>
          <h1>Page not found</h1>
          <p>
            The React app handled this route after Spring Boot served the
            bundled index file.
          </p>
          <Link className="button" to="/">
            Back home
          </Link>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="page-header">
        <h1>Scenarios For Spring Demo</h1>
      </header>

      <section className="panel demo-guide">
        <div>
          <p className="eyebrow">What this demonstrates</p>
          <h2>Scenario testing through normal frontend requests</h2>
          <p>
            The React app always calls the same API endpoints. Values in the
            page URL are forwarded as request headers, so a tester can share a
            link that exercises a backend scenario without changing the frontend
            code or API path.
          </p>
        </div>

        <div className="guide-grid">
          <div>
            <h3>Use it</h3>
            <ol>
              <li>
                Enter <code>topsecret</code> as the authorization value to
                authenticate as "superuser"
              </li>
              <li>
                Enter <code>report-in-progress</code> or <code>report-100</code>{" "}
                as the <code>my-report</code> scenario override.
              </li>
              <li>
                Fetch the report and compare it with the real response when the
                scenario ID is empty.
              </li>
              <li>
                Scenario override is restricted to the superuser. Enter{" "}
                <code>fake-customer-token</code> as the authorization value to
                authenticate as "customer", then try a scenario override.
              </li>
            </ol>
          </div>

          <div>
            <h3>Request behavior</h3>
            <p>
              The API client uses browser query params to modify API request
              headers.
              <br />
              <code>
                ?authorization=topsecret&amp;scenarioOverride=my-report:report-in-progress
              </code>{" "}
              becomes
              <code>Authorization: topsecret</code> and
              <code>X-Scenario-Override: my-report=report-in-progress</code>.
            </p>
            <p>
              The query protocol is reusable: each value uses{" "}
              <code>endpoint-alias:scenario-id</code>, and repeated{" "}
              <code>scenarioOverride</code> params become comma-separated scoped
              overrides. Unrelated endpoints continue to run normally.
            </p>
          </div>
        </div>
      </section>

      <section className="card-grid">
        <article className="endpoint-card">
          <div>
            <p className="eyebrow">GET /api/user</p>
            <h2>User</h2>
          </div>

          <label>
            Authorization
            <input
              type="text"
              value={authorizationHeader}
              onChange={(event) =>
                updateQueryParam("authorization", event.target.value)
              }
              placeholder="topsecret"
            />
          </label>

          <button
            className="button"
            type="button"
            onClick={fetchUser}
            disabled={user.status === "loading"}
          >
            Fetch user
          </button>

          <pre
            className={user.status === "error" ? "error" : ""}
            aria-live="polite"
          >
            {user.value}
          </pre>
        </article>

        <article className="endpoint-card">
          <div>
            <p className="eyebrow">GET /api/my-report</p>
            <h2>Report</h2>
          </div>

          <label>
            Scenario override
            <input
              type="text"
              value={reportScenarioId}
              onChange={(event) =>
                updateScenarioOverride(reportEndpointAlias, event.target.value)
              }
              placeholder="report-in-progress"
            />
          </label>

          <button
            className="button"
            type="button"
            onClick={fetchReport}
            disabled={report.status === "loading"}
          >
            Fetch report
          </button>

          <pre
            className={report.status === "error" ? "error" : ""}
            aria-live="polite"
          >
            {report.value}
          </pre>
        </article>
      </section>
    </main>
  );
}

export default App;
