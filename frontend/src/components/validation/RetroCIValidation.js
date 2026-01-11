import React, { useState, useEffect, useContext } from "react";
import { useLocation } from "react-router-dom";
import Validation from "./Validation";
import { getFromOpenElisServer } from "../utils/Utils";
import { Loading } from "@carbon/react";
import { NotificationContext } from "../layout/Layout";
import { NotificationKinds } from "../common/CustomNotification";
import { useIntl, FormattedMessage } from "react-intl";
import PageBreadCrumb from "../common/PageBreadCrumb";
import { Grid, Column, Section, Heading } from "@carbon/react";

const RetroCIValidation = () => {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const type = searchParams.get("type");
  const test = searchParams.get("test");
  const { addNotification, setNotificationVisible } =
    useContext(NotificationContext);
  const intl = useIntl();

  useEffect(() => {
    fetchData();
  }, [type, test]);

  const fetchData = () => {
    setLoading(true);
    let url = `/rest/validation/retroci?type=${encodeURIComponent(type)}`;
    if (test) {
      url += `&test=${encodeURIComponent(test)}`;
    }

    getFromOpenElisServer(url, (response) => {
      if (response) {
        setResults(response);
      } else {
        addNotification({
          kind: NotificationKinds.error,
          title: intl.formatMessage({ id: "notification.title" }),
          message: intl.formatMessage({ id: "error.loading.data" }),
        });
        setNotificationVisible(true);
      }
      setLoading(false);
    });
  };

  const handleSaveSuccess = () => {
    fetchData();
  };

  if (loading) return <Loading />;

  let breadcrumbs = [{ label: "home.label", link: "/" }];

  const titleId = `result.validation.${type ? type.toLowerCase() : "generic"}.title`;

  return (
    <>
      <PageBreadCrumb breadcrumbs={breadcrumbs} />
      <Grid fullWidth={true}>
        <Column lg={16} md={8} sm={4}>
          <Section>
            <Section>
              <Heading>
                <FormattedMessage
                  id={titleId}
                  defaultMessage={type || "Validation"}
                />
              </Heading>
            </Section>
          </Section>
        </Column>
      </Grid>
      <div className="orderLegendBody">
        {results && (
          <Validation
            results={results}
            params={location.search}
            onSaveSuccess={handleSaveSuccess}
          />
        )}
      </div>
    </>
  );
};

export default RetroCIValidation;
