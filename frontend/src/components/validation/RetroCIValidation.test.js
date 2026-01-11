import React from "react";
import { render, screen, wait } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { IntlProvider } from "react-intl";
import { MemoryRouter, Route } from "react-router-dom";
import RetroCIValidation from "./RetroCIValidation";
import { NotificationContext } from "../layout/Layout";
import { getFromOpenElisServer } from "../utils/Utils";
import { fireEvent } from "@testing-library/dom";

// Mocks
jest.mock("../utils/Utils", () => ({
  getFromOpenElisServer: jest.fn(),
  postToOpenElisServer: jest.fn(),
  convertAlphaNumLabNumForDisplay: (val) => val,
}));

jest.mock("./Validation", () => {
  return function DummyValidation(props) {
    return (
      <div data-testid="validation-component">
        Validation Component
        <button
          data-testid="save-success-trigger"
          onClick={props.onSaveSuccess}
        >
          Trigger Save
        </button>
      </div>
    );
  };
});

const mockAddNotification = jest.fn();
const mockSetNotificationVisible = jest.fn();

const renderComponent = (
  initialEntry = "/RetroCIValidation?type=Immunology",
) => {
  return render(
    <IntlProvider locale="en" messages={{}}>
      <NotificationContext.Provider
        value={{
          addNotification: mockAddNotification,
          setNotificationVisible: mockSetNotificationVisible,
        }}
      >
        <MemoryRouter initialEntries={[initialEntry]}>
          <Route path="/RetroCIValidation">
            <RetroCIValidation />
          </Route>
        </MemoryRouter>
      </NotificationContext.Provider>
    </IntlProvider>,
  );
};

describe("RetroCIValidation", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("fetches data on mount", async () => {
    const mockData = { resultList: [] };
    getFromOpenElisServer.mockImplementation((url, callback) =>
      callback(mockData),
    );

    renderComponent();

    await wait(() => {
      expect(getFromOpenElisServer).toHaveBeenCalledWith(
        expect.stringContaining("/rest/validation/retroci?type=Immunology"),
        expect.any(Function),
      );
    });

    expect(screen.getByTestId("validation-component")).toBeInTheDocument();
  });

  test("handles fetch error", async () => {
    getFromOpenElisServer.mockImplementation((url, callback) =>
      callback(undefined),
    );

    renderComponent();

    await wait(() => {
      expect(getFromOpenElisServer).toHaveBeenCalled();
    });

    expect(mockAddNotification).toHaveBeenCalledWith(
      expect.objectContaining({ kind: "error" }),
    );
  });

  test("refreshes data on save success", async () => {
    const mockData = { resultList: [] };
    getFromOpenElisServer.mockImplementation((url, callback) =>
      callback(mockData),
    );

    renderComponent();

    await wait(() => {
      expect(screen.getByTestId("validation-component")).toBeInTheDocument();
    });

    // Clear initial call
    getFromOpenElisServer.mockClear();
    getFromOpenElisServer.mockImplementation((url, callback) =>
      callback(mockData),
    );

    // Trigger save success
    fireEvent.click(screen.getByTestId("save-success-trigger"));

    await wait(() => {
      expect(getFromOpenElisServer).toHaveBeenCalledWith(
        expect.stringContaining("/rest/validation/retroci"),
        expect.any(Function),
      );
    });
  });
});
