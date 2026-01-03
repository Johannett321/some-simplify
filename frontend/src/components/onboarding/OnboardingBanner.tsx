import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { OnboardingStep1Form } from './OnboardingStep1Form';
import { CheckCircle2, Circle, ChevronDown, ChevronUp, ChevronRight, X } from 'lucide-react';
import { TenantApi } from '@/api';
import apiConfig from '@/config/ApiConfig';
import type { OnboardingStatusTO } from '@/api';
import { useNavigate } from 'react-router-dom';

const DISMISSAL_KEY = 'onboarding-dismissed';

export function OnboardingBanner() {
  const navigate = useNavigate();
  const [tenantApi] = useState(new TenantApi(apiConfig));
  const [status, setStatus] = useState<OnboardingStatusTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [isDismissed, setIsDismissed] = useState(false);
  const [isExpanded, setIsExpanded] = useState(true);
  const [isStep1DialogOpen, setIsStep1DialogOpen] = useState(false);

  useEffect(() => {
    loadOnboardingStatus();

    // Check dismissal from localStorage
    const dismissed = localStorage.getItem(DISMISSAL_KEY);
    if (dismissed === 'true') {
      setIsDismissed(true);
    }
  }, []);

  const loadOnboardingStatus = async () => {
    try {
      const response = await tenantApi.getOnboardingStatus();
      setStatus(response.data);
    } catch (error) {
      console.error('Failed to load onboarding status:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDismiss = () => {
    localStorage.setItem(DISMISSAL_KEY, 'true');
    setIsDismissed(true);
  };

  const handleStep1Success = () => {
    setIsStep1DialogOpen(false); // Close dialog
    loadOnboardingStatus(); // Reload status
  };

  const handleGoToSettings = () => {
    navigate('/innstillinger');
  };

  // Don't show if loading, dismissed, or both steps completed
  if (loading || isDismissed || !status) return null;
  if (status.step1Completed && status.step2Completed) return null;

  return (
    <Card className="mb-6 border-blue-200 bg-blue-50">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div>
            <CardTitle className="text-lg">Kom i gang med SOMESimplify</CardTitle>
            <CardDescription>
              Fullfør disse trinnene for å komme i gang med innholdsplanleggingen
            </CardDescription>
          </div>
          <div className="flex gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setIsExpanded(!isExpanded)}
            >
              {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
            </Button>
            <Button variant="ghost" size="sm" onClick={handleDismiss}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardHeader>

      <Collapsible open={isExpanded}>
        <CollapsibleContent>
          <CardContent className="space-y-3">
            {/* Step 1: Profile */}
            <div
              className={`border rounded-lg p-3 bg-white flex items-center gap-3 ${!status.step1Completed ? 'cursor-pointer hover:bg-gray-50 transition-colors' : ''}`}
              onClick={() => !status.step1Completed && setIsStep1DialogOpen(true)}
            >
              {status.step1Completed ? (
                <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0" />
              ) : (
                <Circle className="h-5 w-5 text-gray-400 flex-shrink-0" />
              )}
              <div className="flex-1">
                <h3 className="font-semibold text-sm">
                  Trinn 1: Fyll ut stedinformasjon
                </h3>
                <p className="text-sm text-gray-600">
                  {status.step1Completed
                    ? 'Ferdig! Du har fylt ut stedinformasjonen.'
                    : 'Hjelp oss å forstå stedet ditt bedre'}
                </p>
              </div>
              {!status.step1Completed && (
                <ChevronRight className="h-5 w-5 text-gray-400 flex-shrink-0" />
              )}
            </div>

            {/* Step 2: Instagram */}
            <div
              className={`border rounded-lg p-3 bg-white flex items-center gap-3 ${!status.step2Completed ? 'cursor-pointer hover:bg-gray-50 transition-colors' : ''}`}
              onClick={() => !status.step2Completed && handleGoToSettings()}
            >
              {status.step2Completed ? (
                <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0" />
              ) : (
                <Circle className="h-5 w-5 text-gray-400 flex-shrink-0" />
              )}
              <div className="flex-1">
                <h3 className="font-semibold text-sm">
                  Trinn 2: Koble til Instagram
                </h3>
                <p className="text-sm text-gray-600">
                  {status.step2Completed
                    ? 'Ferdig! Instagram er koblet til.'
                    : 'Koble til din Instagram-konto for automatisk publisering'}
                </p>
              </div>
              {!status.step2Completed && (
                <ChevronRight className="h-5 w-5 text-gray-400 flex-shrink-0" />
              )}
            </div>
          </CardContent>
        </CollapsibleContent>
      </Collapsible>

      {/* Step 1 Dialog */}
      <Dialog open={isStep1DialogOpen} onOpenChange={setIsStep1DialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Fyll ut stedinformasjon</DialogTitle>
            <DialogDescription>
              Hjelp oss å forstå stedet ditt bedre ved å fylle ut noen grunnleggende opplysninger.
            </DialogDescription>
          </DialogHeader>
          <OnboardingStep1Form onSuccess={handleStep1Success} />
        </DialogContent>
      </Dialog>
    </Card>
  );
}
