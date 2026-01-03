import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { tenantProfileSchema, CONCEPT_OPTIONS, TARGET_AUDIENCE_OPTIONS, type TenantProfileFormData } from '@/schemas/tenantProfileSchema';
import { useState } from 'react';
import { TenantApi } from '@/api';
import apiConfig from '@/config/ApiConfig';
import { toast } from 'sonner';

interface OnboardingStep1FormProps {
  onSuccess: () => void;
}

export function OnboardingStep1Form({ onSuccess }: OnboardingStep1FormProps) {
  const [tenantApi] = useState(new TenantApi(apiConfig));
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<TenantProfileFormData>({
    resolver: zodResolver(tenantProfileSchema),
    defaultValues: {
      targetAudience: [],
    }
  });

  const selectedConcept = watch('concept');
  const selectedTargetAudience = watch('targetAudience') || [];

  const onSubmit = async (data: TenantProfileFormData) => {
    setIsSubmitting(true);
    try {
      await tenantApi.upsertTenantProfile(data);
      toast.success('Profil lagret!');
      onSuccess();
    } catch (error) {
      console.error('Failed to save profile:', error);
      toast.error('Kunne ikke lagre profil');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleTargetAudienceChange = (value: string, checked: boolean) => {
    const current = selectedTargetAudience;
    if (checked) {
      setValue('targetAudience', [...current, value]);
    } else {
      setValue('targetAudience', current.filter(v => v !== value));
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <Label htmlFor="address">Adresse</Label>
        <Input
          id="address"
          type="text"
          placeholder="Eksempel: Karl Johans gate 1, Oslo"
          {...register('address')}
        />
        {errors.address && <p className="text-sm text-red-500 mt-1">{errors.address.message}</p>}
      </div>

      <div>
        <Label htmlFor="concept">Konsept</Label>
        <Select onValueChange={(value) => setValue('concept', value as any)}>
          <SelectTrigger>
            <SelectValue placeholder="Velg konsept" />
          </SelectTrigger>
          <SelectContent>
            {CONCEPT_OPTIONS.map((option) => (
              <SelectItem key={option} value={option}>
                {option}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {errors.concept && <p className="text-sm text-red-500 mt-1">{errors.concept.message}</p>}
      </div>

      {selectedConcept === 'Annet' && (
        <div>
          <Label htmlFor="conceptOther">Spesifiser konsept</Label>
          <Input
            id="conceptOther"
            type="text"
            placeholder="Skriv inn ditt konsept"
            {...register('conceptOther')}
          />
          {errors.conceptOther && <p className="text-sm text-red-500 mt-1">{errors.conceptOther.message}</p>}
        </div>
      )}

      <div>
        <Label>Målgruppe (velg minst én)</Label>
        <div className="space-y-2 mt-2">
          {TARGET_AUDIENCE_OPTIONS.map((option) => (
            <div key={option} className="flex items-center space-x-2">
              <Checkbox
                id={`audience-${option}`}
                checked={selectedTargetAudience.includes(option)}
                onCheckedChange={(checked) => handleTargetAudienceChange(option, checked as boolean)}
              />
              <label htmlFor={`audience-${option}`} className="text-sm cursor-pointer">
                {option}
              </label>
            </div>
          ))}
        </div>
        {errors.targetAudience && <p className="text-sm text-red-500 mt-1">{errors.targetAudience.message}</p>}
      </div>

      {selectedTargetAudience.includes('Annet') && (
        <div>
          <Label htmlFor="targetAudienceOther">Spesifiser målgruppe</Label>
          <Input
            id="targetAudienceOther"
            type="text"
            placeholder="Skriv inn din målgruppe"
            {...register('targetAudienceOther')}
          />
          {errors.targetAudienceOther && <p className="text-sm text-red-500 mt-1">{errors.targetAudienceOther.message}</p>}
        </div>
      )}

      <div>
        <Label htmlFor="websiteUrl">Nettside</Label>
        <Input
          id="websiteUrl"
          type="url"
          placeholder="https://example.com"
          {...register('websiteUrl')}
        />
        {errors.websiteUrl && <p className="text-sm text-red-500 mt-1">{errors.websiteUrl.message}</p>}
      </div>

      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Lagrer...' : 'Lagre og fortsett'}
      </Button>
    </form>
  );
}
